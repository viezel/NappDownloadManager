package dk.napp.downloadmanager;

import java.util.EnumSet;

public enum NetworkTypes {
	None,
	Ethernet,
	MobileBroadbank2G,
	MobileBroadbank3G,
	MobileBroadbank4G,
	Wireless80211;
	
	
	public static final EnumSet<NetworkTypes> Mobile = EnumSet.of(MobileBroadbank2G,MobileBroadbank3G,MobileBroadbank4G);
	public static final EnumSet<NetworkTypes> Any = EnumSet.of(Ethernet,MobileBroadbank2G,MobileBroadbank3G,MobileBroadbank4G,Wireless80211);
}
