package ie.dit.d13122842.main;

public class Enums {
    public enum UITarget {
        CTL_HEAD,           // "Waiting..." or "FITS Cleaning"
        CTL_STATUS,         // "Downloading flat..." or "Ready"
        WRK_HEAD,           // "No work" or "Waiting..." or "0000001.fits"
        WRK_PROGRESS_MAX,   // To send message to set Progress Bar max value
        WRK_PROGRESS_RESET, // To send message to reset Progress Bar value
        WRK_PROGRESS_NEXT,  // To send message to increment Progress Bar value
        WRK_STATUS_1,       // "Star n of n"
        WRK_STATUS_2,       // "Plane n of n"
        WRK_STATUS_3,       // "Downloading..." or "Cleaning..." or "Uploading"
        SUMMARY_1_LABEL,    // "Units Processed:"
        SUMMARY_1,          // "n"
        SUMMARY_2_LABEL,    // "Avg Time per Unit:"
        SUMMARY_2,          // "ns"
        ERROR,
        RESETALL
    }
}
