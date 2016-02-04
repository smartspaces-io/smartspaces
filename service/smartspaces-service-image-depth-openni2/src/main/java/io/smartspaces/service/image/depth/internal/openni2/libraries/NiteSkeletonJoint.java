package io.smartspaces.service.image.depth.internal.openni2.libraries;
import io.smartspaces.service.image.depth.internal.openni2.libraries.NiTE2Library.NiteJointType;

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
public class NiteSkeletonJoint extends StructObject {
	/**
	 * Type of the joint<br>
	 * C type : NiteJointType
	 */
	@Field(0) 
	public IntValuedEnum<NiteJointType > jointType() {
		return this.io.getEnumField(this, 0);
	}
	/**
	 * Type of the joint<br>
	 * C type : NiteJointType
	 */
	@Field(0) 
	public NiteSkeletonJoint jointType(IntValuedEnum<NiteJointType > jointType) {
		this.io.setEnumField(this, 0, jointType);
		return this;
	}
	/**
	 * Position of the joint - in real world coordinates<br>
	 * C type : NitePoint3f
	 */
	@Field(1) 
	public NitePoint3f position() {
		return this.io.getNativeObjectField(this, 1);
	}
	/**
	 * Position of the joint - in real world coordinates<br>
	 * C type : NitePoint3f
	 */
	@Field(1) 
	public NiteSkeletonJoint position(NitePoint3f position) {
		this.io.setNativeObjectField(this, 1, position);
		return this;
	}
	@Field(2) 
	public float positionConfidence() {
		return this.io.getFloatField(this, 2);
	}
	@Field(2) 
	public NiteSkeletonJoint positionConfidence(float positionConfidence) {
		this.io.setFloatField(this, 2, positionConfidence);
		return this;
	}
	/**
	 * Orientation of the joint<br>
	 * C type : NiteQuaternion
	 */
	@Field(3) 
	public NiteQuaternion orientation() {
		return this.io.getNativeObjectField(this, 3);
	}
	/**
	 * Orientation of the joint<br>
	 * C type : NiteQuaternion
	 */
	@Field(3) 
	public NiteSkeletonJoint orientation(NiteQuaternion orientation) {
		this.io.setNativeObjectField(this, 3, orientation);
		return this;
	}
	@Field(4) 
	public float orientationConfidence() {
		return this.io.getFloatField(this, 4);
	}
	@Field(4) 
	public NiteSkeletonJoint orientationConfidence(float orientationConfidence) {
		this.io.setFloatField(this, 4, orientationConfidence);
		return this;
	}
	public NiteSkeletonJoint() {
		super();
	}
	public NiteSkeletonJoint(Pointer pointer) {
		super(pointer);
	}
}