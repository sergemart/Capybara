package com.github.sergemart.mobile.capybara.engine;

public interface CloudEngine {

    /**
     * Check if a current app user is authenticated to the cloud
     */
    boolean isAuthenticated();


    /**
     * Return a name of the authenticated user, or an empty string of not authenticated
     */
    String getCurrentUsername();


    /**
     * Sign in the current user to the cloud
     */
    void signIn();


    /**
     * Sign out the current user off the cloud
     */
    void signOut();

}
