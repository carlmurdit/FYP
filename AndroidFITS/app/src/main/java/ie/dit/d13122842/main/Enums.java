package ie.dit.d13122842.main;

public class Enums {
    public enum UITarget {
        CTLHEAD,        // "Waiting..." or "FITS Cleaning"
        CTLSTATUS,      // "Downloading flat..." or "Ready"
        WRKHEAD,        // "No work" or "Waiting..." or "0000001.fits"
        WRKSTATUS1,     // "Star n of n"
        WRKSTATUS2,     // "Plane n of n"
        WRKSTATUS3,     // "Downloading..." or "Cleaning..." or "Uploading"
        SUMMARY1LABEL,  // "Units Processed:"
        SUMMARY1,       // "n"
        SUMMARY2LABEL,  // "Avg Time per Unit:"
        SUMMARY2,       // "ns"
        ERROR
    }
}
