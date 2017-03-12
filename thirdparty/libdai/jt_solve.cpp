#include <iostream>
#include <dai/alldai.h>  // Include main libDAI header file
#include <dai/jtree.h>


using namespace dai;
using namespace std;


int main( int argc, char *argv[] ) {
    if ( argc != 2 && argc != 3 ) {
        cout << "Usage: " << argv[0] << " <filename.fg> [maxstates]" << endl << endl;
        cout << "Reads factor graph <filename.fg> and runs" << endl;
        cout << "JunctionTree on it." << endl;
        cout << "JunctionTree is only run if a junction tree is found with" << endl;
        cout << "total number of states less than <maxstates> (where 0 means unlimited)." << endl << endl;
        return 1;
    } else {
        // Read FactorGraph from the file specified by the first command line argument
        FactorGraph fg;
        fg.ReadFromFile(argv[1]);
        size_t maxstates = 0;
        if( argc == 3 )
            maxstates = fromString<size_t>( argv[2] );

        // Set some constants
        size_t maxiter = 10000;
        Real   tol = 1e-9;
        size_t verb = 0;

        // Store the constants in a PropertySet object
        PropertySet opts;
        opts.set("maxiter",maxiter);  // Maximum number of iterations
        opts.set("tol",tol);          // Tolerance for convergence
        opts.set("verbose",verb);     // Verbosity (amount of output generated)

        // Bound treewidth for junctiontree
        try {
            boundTreewidth(fg, &eliminationCost_MinFill, maxstates );
        } catch( Exception &e ) {
            if( e.getCode() == Exception::OUT_OF_MEMORY ) {
                cerr << "Out of memory" << endl;
                return -1;
            }
            else
                throw;
        }

        JTree jt, jtmap;
        jt = JTree( fg, opts("updates",string("HUGIN")) );
        jt.init();
        jt.run();
        cout << jt.logZ() << endl;
    }

    return 0;
}
