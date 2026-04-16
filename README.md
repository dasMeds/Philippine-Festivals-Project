# Philippine Festivals Database

**Summary:** A Java Swing desktop application that serves as a database for exploring Philippine festivals.

**Description:** A custom dark-themed Java Swing application designed to catalog cultural events across the Philippines. It categorizes festivals by major island groups and regions, providing detailed historical backgrounds, locations, and direct links to video showcases.

---

## Features

* **Island and Region Navigation**: Browse festivals categorized by the three major island groups (Luzon, Visayas, Mindanao) and their respective regions.
* **Detailed Festival Lore**: View extensive historical descriptions, establishment dates, and cultural significance for major festivals such as Sinulog, Ati-Atihan, Dinagyang, Kadayawan, and MassKara.
* **Dynamic Image Loading**: Automatically fetches and displays festival thumbnail images directly from YouTube using designated video IDs.
* **Interactive UI & Animations**: Features a modern dark theme with custom rounded buttons, smooth card hover zoom animations, and custom styled scrollbars.
* **Video Integration**: Includes a watch feature that opens the system's default web browser to play the festival's associated YouTube video.
* **Custom Modal Overlays**: Utilizes a glass pane to display an overlaid modal containing full festival overviews and descriptions without opening new windows.

---

## Application Flow

1. **Home Page**: Select one of the main island groups: Luzon (The Northern Isles), Visayas (The Central Islands), or Mindanao (The Land of Promise).
2. **Region Page**: Choose a specific region belonging to the selected island group from a grid menu.
3. **Browser & Modal Page**: Browse the grid of festival cards for the selected region. Click a card to open the overview modal to read the history, or click the watch button to view the video.
