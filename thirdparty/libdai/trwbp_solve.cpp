#include <iostream>
#include <dai/alldai.h>  // Include main libDAI header file
#include <dai/trwbp.h>


using namespace dai;
using namespace std;


int main( int argc, char *argv[] ) {
    if ( argc != 2) {
        cout << "Usage: " << argv[0] << " <filename.fg>" << endl << endl;
        cout << "Reads factor graph <filename.fg> and runs" << endl;
        cout << "Tree-reweighted belif propogation on it." << endl << endl;
        return 1;
    } else {
        // Read FactorGraph from the file specified by the first command line argument
        FactorGraph fg;
        fg.ReadFromFile(argv[1]);

        // Set some constants
        size_t maxiter = 1e6;
        Real   tol = 1e-9;
        size_t verb = 0;

        // Store the constants in a PropertySet object
        PropertySet opts;
        opts.set("maxiter",maxiter);  // Maximum number of iterations
        opts.set("tol",tol);          // Tolerance for convergence
        opts.set("verbose",verb);     // Verbosity (amount of output generated)

        TRWBP trwbp;
        // trwbp = TRWBP( fg, opts("updates",string("SEQMAX"))
        //                 ("inference", string("SUMPROD"))
        //                 ("logdomain", true)
        //                 ("damping", 0.5));
        trwbp = TRWBP(fg, opts("updates",string("PARALL"))("logdomain", true));
        trwbp.init();
        trwbp.run();
        cout << trwbp.logZ() << endl;
    }

    return 0;
}

