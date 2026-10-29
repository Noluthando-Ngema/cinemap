Members involved in the making of CineMap
1. Noluthando Ngema
2. Anele Ndlovu
3. Phumelele Ngozo
4. Nosipho Tshabalala


 CineMap Overview

CineMap is a comprehensive Android application built with Kotlin that simplifies the movie-going experience. It allows users to locate the nearest cinema, and see what movies are showing in that cinema. 
Purpose of the app
The primary goal of CineMap is to solve the friction involved in locating a cinema near you and seeing what movies are showing at that cinema. We aimed to create an intuitive platform that offers:
•	Convenience: Users can look up what cinema is nearest to them. 
•	Visual Selection: A selection of which cinema they would like to go to and which movies are showing at those cinemas. 
•	User Experience: A clean, minimalist UI that makes navigation effortless.
Design considerations

1.	User interface (UI) and User experience (UX) 
•	Material Design: We adhered to Google's Material Design guidelines to ensure a familiar and consistent look and feel for Android users.
•	Navigation: We implemented a Bottom Navigation Bar to allow quick access to Home, Bookings, Theatres and Dashboard.
•	Visual Hierarchy: Primary actions (like "Book Now") are highlighted using distinct colours, which is red in this case, while secondary information is de-emphasized.
2.	Responsiveness 
•	The layout is designed using ConstraintLayout to ensure the app looks great on various screen sizes, from small phones to tablets. 
•	For the map on the application, we used OpenMapView
•	Support for Dark Mode, which we called CineMap Noir was implemented to reduce eye strain.
Tech Stack 
•	Language: Kotlin
•	IDE: Android Studios
•	Database: Firebase 
CI/CD & GitHub Actions
We utilized GitHub Actions to automate our development workflow. This ensures code quality and streamlines the build process.
Workflow: Android CI 
Our workflow is defined in .github/workflows/android.yml. It triggers on every push and pull request to the main branch.
What the Action does:
1.	Sets up JDK: Configures Java Development Kit (JDK) version.
2.	Gradle Build: Runs ./gradlew build to compile the Kotlin code.
3.	Linting: Runs ./gradlew lint to check for code quality issues.
4.	Testing: Executes unit tests via ./gradlew test.
REST(API) used: 
1.	Firebase Authentication for sign up and login
2.	TMDB API for the movies, posters and details 
3.	Glide and SharedPreferences load images and save theme and local profile cache


Which AI tools were used? 

For this application, we used Gemini and Claude to help with aligning the app theme with the mockup submitted in Part 1, including identifying and correcting colour codes (e.g. #0F0F13, #FFFFFF) to match the design. We also used both of them to guide me on how to properly implement the dark and light mode switch functionality, ensuring the entire application changes theme. Additionally, we used them to generate the seat selection grid layout and logic, as we were unfamiliar with implementing a dynamic grid for seat booking, to create certain card drawables for the bottom navigation icons and styling, and to assist with implementing a free maps alternative (osmdroid/OpenStreetMap) after finding that Google Maps API requires billing.


https://youtube.com/shorts/hdO_96NAnfc?si=xNKF2vRvsm0x772t 
