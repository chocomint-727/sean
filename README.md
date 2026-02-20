**Sean: Your Personal Record Collection, Digitized**

Welcome to Sean — the social platform for music obsessives! If you’ve ever wished Letterboxd existed for albums instead of movies, you’re in the right place. Log your listens, rate your favorites, and engage with a community that cares as much about B-sides as you do.

**Features**
1. Log & Rate: Keep a running diary of every album you listen to. Give it a star rating and a review.
2. Interact: Look at other users' reviews and top albums. 
3. New Music: Sean's music recommendation system periodically highlights an album for listening.

**Tech Stack**
The technical backbone of Sean is built on a modern, native Android foundation designed for scalability and performance. Developed in Android Studio using Kotlin, the app follows clean architecture principles, utilizing **Hilt** for standardized dependency injection to keep the codebase modular and testable. For the data layer, Sean integrates the **Last.fm** API via **Retrofit** to fetch rich musical metadata, while **Firebase** serves as the comprehensive backend solution—handling real-time data synchronization through **Firestore**, secure user authentication, and cloud storage for profile assets. This combination of a robust native UI and a reactive cloud-based backend ensures a seamless, "always-on" social experience for music fans.
