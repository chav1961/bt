/***
*	fileio.ch
*	Standard definitions for Clipper low level file functions.
*	Copyright (c) 1990 Nantucket Corp.  All Rights Reserved.
*/


// Error value (all functions)
#define F_ERROR			(-1)


// FSEEK() modes

#define FS_SET			0		// Seek from beginning of file
#define FS_RELATIVE		1		// Seek from current file position
#define FS_END			2		// Seek from end of file


// FOPEN() access modes

#define FO_READ			0		// Open for reading (default)
#define FO_WRITE		1		// Open for writing
#define FO_READWRITE            2               // Open for reading or writing

// FOPEN() sharing modes (combine with open mode using +)

#define FO_COMPAT		0		// Compatibility mode (default)
#define FO_EXCLUSIVE            16              // Exclusive
#define FO_DENYWRITE            32              // Prevent other processes from writing
#define FO_DENYREAD		48		// Prevent other processes from reading
#define FO_DENYNONE		64		// (same as FO_SHARED)
#define FO_SHARED		64		// Allow other processes to read or write


// FCREATE() file attribute modes

#define FC_NORMAL		0		// Create normal read/write file (default)
#define FC_READONLY		1		// Create read-only file
#define FC_HIDDEN		2		// Create hidden file
#define FC_SYSTEM		4		// Create system file

// NOTE:  FCREATE() always opens with (FO_READWRITE + FO_COMPAT)

#define F_NAME                 1
#define F_SIZE                 2
#define F_DATE                 3
#define F_TIME                 4
#define F_MASK                 5
#define F_ATTRIB               5
#define F_PATH                 6
#define F_LPATH                7
