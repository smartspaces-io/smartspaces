package io.smartspaces.service.image.depth.internal.openni2.libraries;
import io.smartspaces.service.image.depth.internal.openni2.libraries.NiTE2Library.NitePoseType;

import org.bridj.IntValuedEnum;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : NiteCTypes.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("NiTE2") 
public class NitePoseData extends StructObject {
	/** C type : NitePoseType */
	@Field(0) 
	public IntValuedEnum<NitePoseType > type() {
		return this.io.getEnumField(this, 0);
	}
	/** C type : NitePoseType */
	@Field(0) 
	public NitePoseData type(IntValuedEnum<NitePoseType > type) {
		this.io.setEnumField(this, 0, type);
		return this;
	}
	@Field(1) 
	public int state() {
		return this.io.getIntField(this, 1);
	}
	@Field(1) 
	public NitePoseData state(int state) {
		this.io.setIntField(this, 1, state);
		return this;
	}
	public NitePoseData() {
		super();
	}
	public NitePoseData(Pointer pointer) {
		super(pointer);
	}
}
