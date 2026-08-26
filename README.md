# 📡 Tower Detective

Tower Detective is an Android cellular and GPS monitoring
application combined with RAG and a foundation AI model.

## Features

- GPS location
- GPS accuracy
- Network technology
- Network operator
- Serving cell
- Neighbor cells
- Signal dBm
- MCC
- MNC
- TAC/LAC
- PCI
- RAG knowledge base
- AI network explanation

## Architecture

Android
↓
Capacitor
↓
Native Telephony API
↓
Real device data
↓
RAG
↓
Foundation model
↓
AI analysis

## Important

The application does not access private information from
other phones.

Cellular information depends on Android version, device,
carrier, permissions and network conditions.

The application does not claim that Cell ID automatically
identifies the physical location of a tower.

## Project Structure

See:

docs/architecture.md

## Development

Install JavaScript dependencies:

npm install

Synchronize Android:

npx cap sync android

Build:

cd android

./gradlew assembleDebug
