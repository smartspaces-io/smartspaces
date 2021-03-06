package io.smartspaces.service.image.depth.internal.openni2.libraries;
import io.smartspaces.service.image.depth.internal.openni2.libraries.OpenNI2Library.OniStreamHandle;

import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : OniCTypes.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("OpenNI2") 
public class OniSeek extends StructObject {
	@Field(0) 
	public int frameIndex() {
		return this.io.getIntField(this, 0);
	}
	@Field(0) 
	public OniSeek frameIndex(int frameIndex) {
		this.io.setIntField(this, 0, frameIndex);
		return this;
	}
	/** C type : OniStreamHandle */
	@Field(1) 
	public OniStreamHandle stream() {
		return this.io.getTypedPointerField(this, 1);
	}
	/** C type : OniStreamHandle */
	@Field(1) 
	public OniSeek stream(OniStreamHandle stream) {
		this.io.setPointerField(this, 1, stream);
		return this;
	}
	public OniSeek() {
		super();
	}
	public OniSeek(Pointer pointer) {
		super(pointer);
	}
}
