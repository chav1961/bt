package chav1961.bt.security;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import chav1961.bt.security.interfaces.AlgorithmType;
import chav1961.purelib.basic.Utils;

public class AlgorithmUtils {
	private static final Pattern	TOTAL = Pattern.compile(".*");
	
	public static Iterable<Provider> getProviders() {
		return getProviders(TOTAL);
	}

	public static Iterable<Provider> getProviders(final String pattern) throws IllegalArgumentException{
		if (Utils.checkEmptyOrNullString(pattern)) {
			throw new IllegalArgumentException("Pattern can't be null or empty"); 
		}
		else {
			return getProviders(Pattern.compile(pattern));
		}
	}

	public static Iterable<Provider> getProviders(final Pattern pattern) throws NullPointerException {
		if (pattern == null) {
			throw new NullPointerException("Pattern can't be null"); 
		}
		else {
			final List<Provider>	result = new ArrayList<>();
			
			for(Provider p : Security.getProviders()) {
				if (pattern.matcher(p.getName()).matches()) {
					result.add(p);
				}
			}
			return result;
		}
	}
	
	public static Iterable<AlgorithmDescriptor> getAlgorithms() {
		return getAlgorithms(TOTAL);
	}

	public static Iterable<AlgorithmDescriptor> getAlgorithms(final String pattern) throws NullPointerException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(pattern)) {
			throw new IllegalArgumentException("Pattern can't be null or empty"); 
		}
		else {
			return getAlgorithms(Pattern.compile(pattern));
		}
	}

	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Pattern pattern) throws NullPointerException {
		if (pattern == null) {
			throw new NullPointerException("Pattern can't be null"); 
		}
		else {
			final List<AlgorithmDescriptor>	result = new ArrayList<>();
			
			for(Provider p : Security.getProviders()) {
				for (AlgorithmType type : AlgorithmType.values()) {
					for(Service s : p.getServices()) {
						 if (type.getServiceNamePattern().matcher(s.getType()).matches() && pattern.matcher(s.getAlgorithm()).matches()) {
							 result.add(new AlgorithmDescriptor(p, type, s.getAlgorithm()));
						 }
					}
				}
			}
			return result;
		}
	}

	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Provider provider) throws NullPointerException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else {
			return getAlgorithms(provider, TOTAL);
		}
	}	

	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Provider provider, final String pattern) throws NullPointerException, IllegalArgumentException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(pattern)) {
			throw new IllegalArgumentException("Pattern can't be null or empty"); 
		}
		else {
			return getAlgorithms(provider, Pattern.compile(pattern));
		}
	}
	
	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Provider provider, final Pattern pattern) throws NullPointerException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (pattern == null) {
			throw new NullPointerException("Pattern can't be null"); 
		}
		else {
			final List<AlgorithmDescriptor>	result = new ArrayList<>();
			
			for (AlgorithmType type : AlgorithmType.values()) {
				for(Service s : provider.getServices()) {
					 if (type.getServiceNamePattern().matcher(s.getType()).matches() && pattern.matcher(s.getAlgorithm()).matches()) {
						 result.add(new AlgorithmDescriptor(provider, type, s.getAlgorithm()));
					 }
				}
			}
			return result;
		}
	}

	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Provider provider, final AlgorithmType type) throws NullPointerException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (type == null) {
			throw new NullPointerException("Algorithm type can't be null"); 
		}
		else {
			return getAlgorithms(provider, type, TOTAL);
		}
	}	

	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Provider provider, final AlgorithmType type, final String pattern) throws NullPointerException, IllegalArgumentException{
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (type == null) {
			throw new NullPointerException("Algorithm type can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(pattern)) {
			throw new IllegalArgumentException("Pattern can't be null or empty"); 
		}
		else {
			return getAlgorithms(provider, type, Pattern.compile(pattern));
		}
	}
	
	public static Iterable<AlgorithmDescriptor> getAlgorithms(final Provider provider, final AlgorithmType type, final Pattern pattern) throws NullPointerException {
		if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (type == null) {
			throw new NullPointerException("Algorithm type can't be null"); 
		}
		else if (pattern == null) {
			throw new NullPointerException("Pattern can't be null"); 
		}
		else {
			final List<AlgorithmDescriptor>	result = new ArrayList<>();
			
			for(Service s : provider.getServices()) {
				 if (type.getServiceNamePattern().matcher(s.getType()).matches() && pattern.matcher(s.getAlgorithm()).matches()) {
					 result.add(new AlgorithmDescriptor(provider, type, s.getAlgorithm()));
				 }
			}
			return result;
		}
	}
	
	public static class AlgorithmDescriptor {
		private final Provider		provider;
		private final AlgorithmType	type;
		private final String		name;
		
		private AlgorithmDescriptor(final Provider provider, final AlgorithmType type, final String name) {
			this.provider = provider;
			this.type = type;
			this.name = name;
		}

		public Provider getProvider() {
			return provider;
		}

		public AlgorithmType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((provider == null) ? 0 : provider.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			AlgorithmDescriptor other = (AlgorithmDescriptor) obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			if (provider == null) {
				if (other.provider != null) return false;
			} else if (!provider.equals(other.provider)) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public String toString() {
			return "AlgorithmDescriptor [provider=" + provider + ", type=" + type + ", name=" + name + "]";
		}
	}
}
