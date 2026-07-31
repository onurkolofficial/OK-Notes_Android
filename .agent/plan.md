# Project Plan

Bir not uygulaması oluşturacağız. Üst navigasyonda sol köşede uygulama başlığı, sağ köşede "Ayarlar" butonu olacak. Hemen altında "Yeni not ekle" butonu olacak.

## Project Brief

# Project Brief: OK Notes

## Features
- **Top Navigation Header**: Displays the app title on the top-left and a Settings icon button on the top-right.
- **Quick Note Creation**: Prominent "Add New Note" button positioned directly beneath the top navigation bar for immediate note creation.
- **Note List View**: Displays existing notes in a clean, scannable layout showing note titles and previews.
- **Settings Screen**: Dedicated screen accessed via the navigation bar to manage application settings and preferences.

## High-Level Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library
- **Asynchronous & State Handling**: Kotlin Coroutines & StateFlow
- **Architecture**: MVVM with ViewModel and Unidirectional Data Flow (UDF)

## Implementation Steps

### Task_1_DataAndViewModelSetup: Define Note model, local repository/data source for managing notes and settings, and ViewModels with StateFlow for UI state.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - Note model and repository implemented
  - ViewModels for Notes and Settings implemented
  - Data flow and state management ready
- **StartTime:** 2026-07-31 19:05:37 TRT

### Task_2_UIAndNavigationSetup: Implement Main Screen with Top Navigation (Title and Settings icon), Quick 'Add New Note' button under header, Notes List view, and Settings screen UI with navigation.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - Top bar with Title on left and Settings button on right functional
  - 'Add New Note' button displayed directly beneath top bar
  - Notes List view and Settings screen created
  - Navigation between Note List and Settings screen working

### Task_3_NoteCreationAndSettingsIntegration: Implement Note creation/editing interface, connect Quick Note button to open creation view, wire ViewModel state to persist and display notes in the list, and hook up Settings preferences.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - Clicking 'Add New Note' allows user to create a new note
  - Newly created notes appear in the Note List view
  - Settings preference changes reflected in state
  - Full note creation and viewing flow complete

### Task_4_RunAndVerify: Run and verify the OK Notes app. Ensure top navigation, quick note creation, note list, and settings screen operate cleanly and stably.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - build pass
  - make sure all existing tests pass
  - app does not crash
  - critic_agent verifies application stability and alignment with user requirements

