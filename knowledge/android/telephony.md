# Android Telephony

Tower Detective uses Android telephony APIs to request
cellular information from the local device.

The application does not obtain private information from
other phones.

Information available to an application depends on:

- Android version
- device manufacturer
- carrier
- SIM/network state
- granted permissions
- radio implementation

The application must treat unavailable information as unavailable
rather than inventing values.
