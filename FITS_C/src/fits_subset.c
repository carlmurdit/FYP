/*
 Build:
 gcc -o bin/fits_subset fits_subset.c -L/Users/carl/My\ Cubby/DIT/FYP/Data/CFITSIO/cfitsio/ -lcfitsio -lm

 Call:
 bin/fits_subset fits/0000801.fits[100:105,200:210] 5
 */

#include <string.h>
#include <stdio.h>
#include "fitsio.h"

/*
 ** Print entire file or rectangles within 2D images
 */

int main(int argc, char *argv[]) {
	fitsfile *afptr; /* FITS file pointers */
	int status = 0; /* CFITSIO status value MUST be initialized to zero! */
	int anaxis, check = 1, ii;
	int plane = -1; // parameter
	long npixels = 1, firstpix[3] = { 1, 1, 1 };
	long anaxes[3] = { 1, 1, 1 };
	double *apix;

	if (argc != 3) {
		printf("Usage: fits_subset fitsfile[startX:startY,endX:endY] plane\n");
		printf("\n");
		printf("Example: Print pixels in the rectangle bound by "
				"X87, Y181 and X166, Y260 in plane 5:\n");
		printf("  ./fits_subset fits/0000801.fits[87:166,181:260] 5 \n");
		return (0);
	}
	
//	for(int i=0;i<argc;i++)
//			printf("Argument %d: '%s'\n", (i+1), argv[i]);

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
		check = 0;
	}

	npixels = anaxes[0]; /* no. of pixels to read in each row */
	apix = (double *) malloc(npixels * sizeof(double)); /* mem for 1 row */

	if (apix == NULL) {
		printf("Memory allocation error\n");
		return (1);
	}

	/* loop over the requested plane of the cube */
	for (firstpix[1] = 1; firstpix[1] <= anaxes[1]; firstpix[1]++) {
		/* Read both images as doubles, regardless of actual datatype.  */
		/* Give starting pixel coordinate and no. of pixels to read.    */
		/* This version does not support undefined pixels in the image. */

		if (fits_read_pix(afptr, TDOUBLE, firstpix, npixels, NULL, apix, NULL, &status))
			break; /* jump out of loop on error */
		for (ii = 0; ii < npixels; ii++)
			printf("p%d r%ld c%d %*.*f\n",
					plane, firstpix[1], ii, 11, 10, apix[ii]);
	}

	free(apix);

	fits_close_file(afptr, &status);

	if (status)
		fits_report_error(stderr, status); /* print any error message */
	return (status);
}
