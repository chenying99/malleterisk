package ft.selection;

import cc.mallet.types.InstanceList;

public interface IFilter {
	InstanceList filter(int n, InstanceList instances);
	String getDescription();
}
