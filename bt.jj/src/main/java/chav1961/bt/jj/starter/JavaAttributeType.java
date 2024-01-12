package chav1961.bt.jj.starter;

import chav1961.purelib.cdb.JavaClassVersion;

enum JavaAttributeType {
	ConstantValue(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute),
	Code(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute),
	StackMapTable(new JavaClassVersion(50,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute),
	Exceptions(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile),
	InnerClasses(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile),
	EnclosingMethod(new JavaClassVersion(49,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile),
	Synthetic(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile),
	Signature(new JavaClassVersion(49,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile),
	SourceFile(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging),
	SourceDebugExtension(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Debugging),
	LineNumberTable(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging),
	LocalVariableTable(new JavaClassVersion(45,3), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging),
	LocalVariableTypeTable(new JavaClassVersion(47,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Debugging),
	Deprecated(new JavaClassVersion(45,3), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile),
	RuntimeVisibleAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	RuntimeInvisibleAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	RuntimeVisibleParameterAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	RuntimeInvisibleParameterAnnotations(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	RuntimeVisibleTypeAnnotations(new JavaClassVersion(52,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	RuntimeInvisibleTypeAnnotations(new JavaClassVersion(52,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	AnnotationDefault(new JavaClassVersion(49,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	BootstrapMethods(new JavaClassVersion(51,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute),
	MethodParameters(new JavaClassVersion(52,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile),
	Module(new JavaClassVersion(53,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Execute),
	ModulePackages(new JavaClassVersion(53,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile),
	ModuleMainClass(new JavaClassVersion(53,0), JavaAttributeSeverity.NonCritical, JavaAttributeProcessing.Compile),
	NestHost(new JavaClassVersion(55,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute),
	NestMembers(new JavaClassVersion(55,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute),
	Record(new JavaClassVersion(60,0), JavaAttributeSeverity.Critical4SE, JavaAttributeProcessing.Compile),
	PermittedSubclasses(new JavaClassVersion(61,0), JavaAttributeSeverity.Critical4VM, JavaAttributeProcessing.Execute);
	
	private final JavaClassVersion			fromVersion;
	private final JavaAttributeSeverity		severity; 
	private final JavaAttributeProcessing	processing;
	
	JavaAttributeType(final JavaClassVersion fromVersion, final JavaAttributeSeverity severity, final JavaAttributeProcessing processing) {
		this.fromVersion = fromVersion;
		this.severity = severity;
		this.processing = processing;
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
}
