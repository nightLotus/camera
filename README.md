# Overview
This application takes 10 photos of your face in a 10-second interval using the front-facing camera.

Possible attacks that are mitigated:
- Add a random salt to the file name so that changing the system's clock can't overwrite previous files
- Encrypt the image files. Obvious care needs to be taken to make sure the encryption is implemented correctly, otherwise it's meaningless, but I attempted AES encryption from the Java API for example here.

# Running
This app requires only AndroidStudio with the appropriate SDKs and SDK tools. See app/build.gradle for specific versions.

# Further Considerations
I haven't work much with Cameras on Android.

Modern phones should have no issue taking photos at a 500ms delay, however, it's entirely possible that the camera won't be able to keep up with a 500ms photo-taking rate. We do the best that we can here using postDelay and checking the elapsed time for each photo. It's likely better to use video capture and choose frames from the correct positions aligning with 500ms delay.

I'm using the Android Camera APIs (as opposed to Camera**2**), which Google's documentation assures me is deprecated but is still used for the official developer guide. Given more time I would study the appropriate camera API to use as well.

Photos are stored in the application's local storage (/data/data/<app-package>), which is not readable or writable by other apps (including the shell user). We don't consider root access to the device as that makes security nearly impossible with things like Xposed or other app hacking, although good encryption may help here.

If encrypting the photos, we need to use other authentication methods, such as PIN, password, or fingerprint, and not a randomly generated key, as I did here. Or, if we use a randomly generated key, it needs to be done consistently or in a manner that makes it secure and recoverable.
