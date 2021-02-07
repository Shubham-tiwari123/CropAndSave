# Crop&Save
This is a simple Android Application which provides functions like image crop and save in gallery or on server . 


## Tech Stack
<img src='https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white' height='35'/> <img src='https://img.shields.io/badge/Servlet-ED8B00?style=for-the-badge&logo=java&logoColor=white' height='35'/>


## Run project
- Pull the repository from Github
- Use **main** branch for android and **server** branch for running server
- Setup tomcat for server
- Create a folder to store image in server
- Start the tomcat server
- Run the android on emulator or on physical device
- Keep the laptop and andoid phone connected to save internet connection
- Change the **BASE_URL** in **RetrofitClient** according to your **ip-address** of the server

## Libraries used
- Crop Image Library: 
        
        'com.theartofdev.edmodo:android-image-cropper:2.8.+'
- Send Image to server: **Retrofit**  

        'com.squareup.retrofit2:retrofit:2.5.0'
        'com.squareup.retrofit2:converter-gson:2.1.0'

## Implemented Functions

**1. captureImageCamera:** This function capture the image from camera and display it in ImageView

**2. captureImageGallery:** This function pick image from the gallery and display it in ImageView

**3. cropImage:** This function crops the image taken from camera or from gallery

**4. saveImageGallery:** This function store the image into the phone gallery

**5. saveImageServer:** This function store the image on the server

