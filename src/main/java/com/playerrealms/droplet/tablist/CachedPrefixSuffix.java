package com.playerrealms.droplet.tablist;

public class CachedPrefixSuffix extends PrefixSuffix {

	private final String prefix, suffix;
	
	public CachedPrefixSuffix(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public CachedPrefixSuffix(PrefixSuffix ps) {
		prefix = ps.getPrefix();
		suffix = ps.getSuffix();
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CachedPrefixSuffix) {
			CachedPrefixSuffix sf = (CachedPrefixSuffix) obj;
			
			return sf.prefix.equals(prefix) && sf.suffix.equals(suffix);
		}else if(obj instanceof PrefixSuffix) {
			PrefixSuffix ps = (PrefixSuffix) obj;
			
			return ps.getPrefix().equals(prefix) && ps.getSuffix().equals(suffix);
		}
		return super.equals(obj);
	}

}
