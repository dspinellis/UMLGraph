package gr.spinellis.iface.classes;

import java.io.Serializable;

public interface Face {}

interface SubFace extends Face, Serializable {}

interface SubSubFace extends SubFace {}

interface Independent extends Serializable {}

class DirectImplementor implements Face {}

class FarImplementor implements SubSubFace {}

class NonCompliant {}