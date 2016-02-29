/*

 gcc -o my_ShowData_2 my_ShowData_2.c -L/Users/carl/My\ Cubby/DIT/FYP/Data/CFITSIO/cfitsio/ -lcfitsio -lm
 ./my_ShowData_2 fits/0000801.fits[100:105,200:210] 5 results.txt
 ./my_ShowData_2 fits/0000801.fits[100:105,200:210] 5 /Users/carl/Dropbox/FYP/Eclipse/APIServer/APIServer/WebContent/my_ShowData_2
This version writes to file (my_ShowData was hanging for large boxes).
 */

#include <string.h>
#include <stdio.h>
#include "fitsio.h"

FILE *fp = NULL;

static FILE *open_result_file(const char *prefix) {
    // const char *suffix = ".result";
    const char *suffix = "";
    char *filename = strdup(prefix);
    filename = realloc(filename, strlen(prefix) + strlen(suffix) + 1);
    strcat(filename, suffix);
    FILE *fp = fopen(filename, "w");
    free(filename);
    return fp;
}

/*
 ** Print entire file or rectangles within 2D images
 */

int main(int argc, char *argv[]) {
	fitsfile *afptr; /* FITS file pointers */
	int status = 0; /* CFITSIO status value MUST be initialized to zero! */
	int anaxis, ii;
	int plane = -1; // parameter
	long npixels = 1, firstpix[3] = { 1, 1, 1 };
	long anaxes[3] = { 1, 1, 1 };
	double *apix;

	if (argc != 4) {
		printf("Usage: my_ShowData fitsfile[startX:startY,endX:endY] plane resultFile\n");
		printf("\n");
		printf("Example: Print pixels in the rectangle bound by "
				"X100, Y105 and X200, Y210 in plane 5:\n");
		printf("  ./my_ShowData fits/0000801.fits[100:105,200:210] 5 results.txt\n");
		for(int i=0;i<argc;i++)
				printf("Argument %d: %s\n", (i+1), argv[i]);
		return (0);
	}
	for(int i=0;i<argc;i++)
			printf("Argument %d: '%s'\n", (i+1), argv[i]);

	plane = atoi(argv[2]);

	fits_open_file(&afptr, argv[1], READONLY, &status); /* open input images */
	if (status) {
		fits_report_error(stderr, status); /* print error message */
		return (status);
	}

	fits_get_img_dim(afptr, &anaxis, &status); /* read dimensions */
	fits_get_img_size(afptr, 3, anaxes, &status);

	if (status) {
		fits_report_error(stderr, status); /* print error message */
		return (status);
	}

	if (anaxis > 3) {
		printf("Error: images with > 3 dimensions are not supported\n");
		return 1;
	}

	npixels = anaxes[0]; /* no. of pixels to read in each row */
	apix = (double *) malloc(npixels * sizeof(double)); /* mem for 1 row */

	if (apix == NULL) {
		printf("Memory allocation error\n");
		return (1);
	}


	// fp = open_result_file("/Users/carl/Dropbox/FYP/Eclipse/APIServer/APIServer/WebContent/my_ShowData_2");
	fp = open_result_file(argv[3]);

	/* loop over the requested plane of the cube */
	firstpix[2] = plane;
	/* loop over all rows of the plane */
	for (firstpix[1] = 1; firstpix[1] <= anaxes[1]; firstpix[1]++) {
		/* Read both images as doubles, regardless of actual datatype.  */
		/* Give starting pixel coordinate and no. of pixels to read.    */
		/* This version does not support undefined pixels in the image. */

		if (fits_read_pix(afptr, TDOUBLE, firstpix, npixels, NULL, apix,
		NULL, &status))
			break; /* jump out of loop on error */
		for (ii = 0; ii < npixels; ii++)
//			printf("p%ld r%ld c%d %*.*f\n",
//					firstpix[2], firstpix[1], ii, 11, 10, apix[ii]);
			fprintf(fp, "p%ld r%ld c%d %*.*f\n",
					firstpix[2], firstpix[1], ii, 11, 10, apix[ii]);
	}

	fclose(fp);

	free(apix);

	fits_close_file(afptr, &status);

	if (status)
		fits_report_error(stderr, status); /* print any error message */
	return (status);
}