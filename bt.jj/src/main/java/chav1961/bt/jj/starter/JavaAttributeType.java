package chav1961.bt.jj.starter;

import chav1961.purelib.cdb.JavaClassVersion;

enum JavaAttributeType {
	ConstantValue(new JavaClassVersion(45,3)),
	Code(new JavaClassVersion(45,3)),
	StackMapTable(new JavaClassVersion(50,0)),
	Exceptions(new JavaClassVersion(45,3)),
	InnerClasses(new JavaClassVersion(45,3)),
	EnclosingMethod(new JavaClassVersion(49,0)),
	Synthetic(new JavaClassVersion(45,3)),
	Signature(new JavaClassVersion(49,0)),
	SourceFile(new JavaClassVersion(45,3)),
	SourceDebugExtension(new JavaClassVersion(49,0)),
	LineNumberTable(new JavaClassVersion(45,3)),
	LocalVariableTable(new JavaClassVersion(45,3)),
	LocalVariableTypeTable(new JavaClassVersion(47,0)),
	Deprecated(new JavaClassVersion(45,3)),
	RuntimeVisibleAnnotations(new JavaClassVersion(49,0)),
	RuntimeInvisibleAnnotations(new JavaClassVersion(49,0)),
	RuntimeVisibleParameterAnnotations(new JavaClassVersion(49,0)),
	RuntimeInvisibleParameterAnnotations(new JavaClassVersion(49,0)),
	RuntimeVisibleTypeAnnotations(new JavaClassVersion(52,0)),
	RuntimeInvisibleTypeAnnotations(new JavaClassVersion(52,0)),
	AnnotationDefault(new JavaClassVersion(49,0)),
	BootstrapMethods(new JavaClassVersion(51,0)),
	MethodParameters(new JavaClassVersion(52,0)),
	Module(new JavaClassVersion(53,0)),
	ModulePackages(new JavaClassVersion(53,0)),
	ModuleMainClass(new JavaClassVersion(53,0)),
	NestHost(new JavaClassVersion(55,0)),
	NestMembers(new JavaClassVersion(55,0)),
	Record(new JavaClassVersion(60,0)),
	PermittedSubclasses(new JavaClassVersion(61,0));
	
	private final JavaClassVersion	fromVersion;
	
	JavaAttributeType(final JavaClassVersion	fromVersion) {
		this.fromVersion = fromVersion;
	}
	
	public int getFromMajor() {
		return fromVersion.major;
	}

	public int getFromMinor() {
		return fromVersion.minor;
	}
	
	public JavaClassVersion getFromClassVersion() {
		return fromVersion;
	}
}
