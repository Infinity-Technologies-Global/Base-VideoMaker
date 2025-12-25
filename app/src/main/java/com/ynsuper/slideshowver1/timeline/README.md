# 🎬 ExoPlayer Timeline Demo

Demo implementation của kiến trúc ExoPlayer + Timeline View theo tài liệu architecture.

## 📋 Tính Năng

- ✅ Chọn ảnh và video từ gallery
- ✅ ExoPlayer để play video
- ✅ Timeline View với multi-track (Video, Audio, Overlay, Text)
- ✅ Seek timeline để preview
- ✅ Play/Pause controls

## 🏗️ Kiến Trúc

### Data Models
- **Clip**: Đại diện cho một media clip trong timeline
- **Track**: Track chứa các clips (Video, Audio, Overlay, Text)
- **TimelineController**: Quản lý timeline và các tracks

### Components
- **MediaSourceManager**: Quản lý ExoPlayer instances và image bitmaps
- **TimelineView**: Custom view để hiển thị timeline với các clips
- **ExoPlayerTimelineActivity**: Activity demo chính
- **ExoPlayerTimelineViewModel**: ViewModel quản lý logic

## 🚀 Cách Sử Dụng

1. **Chọn Media**: 
   - Click "Create Slideshow" trong MainActivity
   - Chọn ảnh và video từ gallery
   - App sẽ tự động navigate đến ExoPlayerTimelineActivity

2. **Timeline View**:
   - Hiển thị các tracks: Video, Audio, Overlay, Text
   - Mỗi clip được hiển thị dưới dạng rectangle với màu sắc khác nhau
   - Video clips: màu xanh lá (#4CAF50)
   - Image clips: màu xanh dương (#2196F3)

3. **Controls**:
   - **Play/Pause**: Click button để play/pause
   - **Seek**: Click hoặc drag trên timeline để seek
   - **Playhead**: Đường màu đỏ hiển thị vị trí hiện tại

## 📁 File Structure

```
timeline/
├── model/
│   ├── Clip.kt          # Clip data class
│   └── Track.kt         # Track data class
├── view/
│   ├── TimelineView.kt  # Custom timeline view
│   └── ExoPlayerTimelineActivity.kt  # Main activity
├── viewmodel/
│   └── ExoPlayerTimelineViewModel.kt  # ViewModel
├── TimelineController.kt  # Timeline controller
├── MediaSourceManager.kt  # Media source manager
└── README.md
```

## 🔄 Flow

```
User chọn ảnh/video
    ↓
MainViewModel.startImagePicker()
    ↓
Navigate to ExoPlayerTimelineActivity
    ↓
ExoPlayerTimelineViewModel.loadMediaItems()
    ↓
Tạo Clips từ ImageModel
    ↓
Add clips vào TimelineController
    ↓
Setup ExoPlayer cho video clips
    ↓
TimelineView hiển thị timeline
    ↓
User có thể play/pause/seek
```

## 📚 Dựa Trên Tài Liệu

- **MASTER_ARCHITECTURE.md**: Luồng tổng thể
- **ARCHITECTURE_CAPCUT_STYLE.md**: Multi-track timeline structure
- **ARCHITECTURE_HUONG2.md**: ExoPlayer integration

## ⚠️ Lưu Ý

- Demo này chỉ implement phần cơ bản
- Chưa có effects, filters, transitions
- Chưa có export video
- Image preview chưa được implement (chỉ có video player)

## 🔮 Next Steps

1. Implement image preview trong PlayerView
2. Add effects và filters
3. Add transitions giữa clips
4. Implement export video
5. Add audio track support
6. Add overlay và text layers

