Overview

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




