package opticalraytracer;

final public class HelpState {
	int scrollBar = 0;
	int selectStart = 0;
	int selectEnd = 0;

	public HelpState(int sb, int ss, int se) {
		scrollBar = sb;
		selectStart = ss;
		selectEnd = se;
	}
}