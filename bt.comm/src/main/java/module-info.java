module chav1961.bt.comm {
	requires transitive chav1961.purelib;
	requires java.base;
	requires javax.comm;
	
	uses javax.comm.CommDriver;
	provides javax.comm.CommDriver with chav1961.bt.comm.FazecastWrapper;
}
