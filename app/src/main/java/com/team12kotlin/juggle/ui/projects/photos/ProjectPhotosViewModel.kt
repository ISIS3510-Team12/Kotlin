package com.team12kotlin.juggle.ui.projects.photos

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.DevicePhoto
import com.team12kotlin.juggle.ui.dto.ProjectPhoto
import com.team12kotlin.juggle.ui.utils.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.UUID

private const val MAX_PARALLEL_UPLOADS = 3

enum class GalleryFilter(val label: String) { ALL("All"), SAVED("Saved") }

// A photo that is being uploaded, or that failed to upload.
data class PhotoUpload(
    val uri: Uri,
    val clientUploadId: String,
    val caption: String? = null,
    val failed: Boolean = false
)

data class ProjectPhotosUiState(
    val projectName: String = "",
    // Only the uploader can delete a photo.
    val currentUserId: String = "",
    val photos: List<ProjectPhoto> = emptyList(),
    val saved: List<DevicePhoto> = emptyList(),
    val recent: List<DevicePhoto> = emptyList(),
    val uploads: List<PhotoUpload> = emptyList(),
    val filter: GalleryFilter = GalleryFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notice: String? = null
)

class ProjectPhotosViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = Dependencies.projectPhotoRepository
    private val projectRepository = Dependencies.projectRepository
    private val gallery = Dependencies.deviceGallery

    private val projectId: Int = savedStateHandle.get<Int>("projectId") ?: 0

    private val uploadSlots = Semaphore(MAX_PARALLEL_UPLOADS)

    private val _uiState = MutableStateFlow(
        ProjectPhotosUiState(currentUserId = Dependencies.authRepository.currentUserId())
    )
    val uiState: StateFlow<ProjectPhotosUiState> = _uiState.asStateFlow()

    fun load() {
        loadSaved()
        viewModelScope.launch {
            attempt(onError = {}) {
                val name = projectRepository.getProject(projectId).name
                _uiState.update { it.copy(projectName = name) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            attempt(
                onError = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage() ?: "Connect to the internet to see the photos."
                        )
                    }
                }
            ) {
                val photos = repository.getPhotos(projectId)
                _uiState.update { it.copy(photos = photos, isLoading = false) }
            }
        }
    }

    fun onFilterSelected(filter: GalleryFilter) {
        _uiState.update { it.copy(filter = filter) }
        if (filter == GalleryFilter.SAVED) loadSaved()
    }

    fun loadRecent() {
        viewModelScope.launch {
            attempt(onError = {}) {
                val recent = gallery.recentPhotos()
                _uiState.update { it.copy(recent = recent) }
            }
        }
    }

    // Uploads the photos in parallel, at most MAX_PARALLEL_UPLOADS at a time.
    fun upload(uris: List<Uri>, caption: String?) {
        uris.forEach { uri -> start(PhotoUpload(uri, UUID.randomUUID().toString(), caption)) }
    }

    fun retry(upload: PhotoUpload) = start(upload.copy(failed = false))

    private fun start(upload: PhotoUpload) {
        val id = upload.clientUploadId
        _uiState.update { state ->
            state.copy(
                uploads = state.uploads.filterNot { it.clientUploadId == id } + upload,
                filter = GalleryFilter.ALL
            )
        }
        viewModelScope.launch {
            uploadSlots.withPermit {
                attempt(
                    onError = {
                        _uiState.update { state ->
                            state.copy(uploads = state.uploads.map {
                                if (it.clientUploadId == id) it.copy(failed = true) else it
                            })
                        }
                    }
                ) {
                    val photo = repository.upload(projectId, upload.uri, id, upload.caption)
                    _uiState.update { state ->
                        state.copy(
                            uploads = state.uploads.filterNot { it.clientUploadId == id },
                            photos = listOf(photo) + state.photos.filterNot { it.id == photo.id },
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun save(photo: ProjectPhoto) {
        viewModelScope.launch {
            attempt(onError = { showNotice("Couldn't save the photo.") }) {
                val bytes = repository.download(projectId, photo.id)
                gallery.save(projectId, "juggle-${photo.id}-${System.currentTimeMillis()}.jpg", bytes)
                showNotice("Saved to your gallery.")
                loadSaved()
            }
        }
    }

    fun delete(photo: ProjectPhoto) {
        viewModelScope.launch {
            attempt(onError = { showNotice("Couldn't delete the photo.") }) {
                repository.delete(projectId, photo.id)
                _uiState.update { state -> state.copy(photos = state.photos.filterNot { it.id == photo.id }) }
            }
        }
    }

    fun onNoticeShown() = _uiState.update { it.copy(notice = null) }

    private fun showNotice(message: String) = _uiState.update { it.copy(notice = message) }

    private fun loadSaved() {
        viewModelScope.launch {
            attempt(onError = {}) {
                val saved = gallery.savedPhotos(projectId)
                _uiState.update { it.copy(saved = saved) }
            }
        }
    }

    private suspend fun attempt(onError: (Exception) -> Unit, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onError(e)
        }
    }
}
