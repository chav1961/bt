package chav1961.bt.jj.starter;

import chav1961.purelib.cdb.JavaClassVersion;

enum JavaAttributeType {
	ConstantValue(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	Code(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InMethod),
	StackMapTable(new JavaClassVersion(50,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	Exceptions(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	InnerClasses(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	EnclosingMethod(new JavaClassVersion(49,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	Synthetic(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	Signature(new JavaClassVersion(49,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	SourceFile(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging, JavaAttributeLocation.InField),
	SourceDebugExtension(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Debugging, JavaAttributeLocation.InField),
	LineNumberTable(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging, JavaAttributeLocation.InField),
	LocalVariableTable(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging, JavaAttributeLocation.InField),
	LocalVariableTypeTable(new JavaClassVersion(47,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging, JavaAttributeLocation.InField),
	Deprecated(new JavaClassVersion(45,3), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	RuntimeVisibleAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	RuntimeInvisibleAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	RuntimeVisibleParameterAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	RuntimeInvisibleParameterAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	RuntimeVisibleTypeAnnotations(new JavaClassVersion(52,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	RuntimeInvisibleTypeAnnotations(new JavaClassVersion(52,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	AnnotationDefault(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	BootstrapMethods(new JavaClassVersion(51,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	MethodParameters(new JavaClassVersion(52,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	Module(new JavaClassVersion(53,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	ModulePackages(new JavaClassVersion(53,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	ModuleMainClass(new JavaClassVersion(53,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	NestHost(new JavaClassVersion(55,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	NestMembers(new JavaClassVersion(55,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InField),
	Record(new JavaClassVersion(60,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile, JavaAttributeLocation.InField),
	PermittedSubclasses(new JavaClassVersion(61,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Always, JavaAttributeLocation.InField);
	
	private final JavaClassVersion			fromVersion;
	private final JavaAttributeSeverity		severity; 
	private final JavaAttributeProcessing	processing;
	private final JavaAttributeLocation		location;
	
	JavaAttributeType(final JavaClassVersion fromVersion, final JavaAttributeSeverity severity, final JavaAttributeProcessing processing, final JavaAttributeLocation location) {
		this.fromVersion = fromVersion;
		this.severity = severity;
		this.processing = processing;
		this.location = location;
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
	
	public JavaAttributeSeverity getSeverity() {
		return severity;
	}
	
	public JavaAttributeProcessing getProcessing() {
		return processing;
	}

	public boolean isApplicableFor(final JavaAttributeProcessing processing) {
		return this.processing == processing;
	}
	
	public JavaAttributeLocation getLocation() {
		return location;
	}
	
	public boolean isApplicableFor(final JavaAttributeLocation location) {
		return this.location == location;
	}
}
