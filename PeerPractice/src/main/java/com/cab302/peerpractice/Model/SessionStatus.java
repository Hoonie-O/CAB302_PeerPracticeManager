package com.cab302.peerpractice.Model;

/**
 * Status for study sessions - I have keep it simple for now.
 */
public enum SessionStatus {
    PLANNED,    // Just created, people can join
    ACTIVE,     // Currently happening
    COMPLETED,  // Finished
    CANCELLED   // Didn't happen
}