package com.example.smk.airnow.Location;

// https://www.androidpub.com/1043970

public class GeoPoint {
	double X, Y, Z;
	double geoX, geoY;
	double tmX, tmY;

	public GeoPoint() {
		super();
	}

	public GeoPoint(double X, double Y) {
		this.X = X;
		this.Y = Y;
	}

	public GeoPoint(double X, double Y, double Z) {
		this.X = X;
		this.Y = Y;
		this.Z = Z;
	}

	public GeoPoint(double geoX, double geoY, double tmX, double tmY) {
		this.geoX = geoX;
		this.geoY = geoY;
		this.tmX = tmX;
		this.tmY = tmY;
	}
	
	public double X() { return X; }

	public double Y() { return Y; }

	public double geoX() { return geoX; }

	public double geoY() { return geoY; }

	public double tmX() { return tmX; }

	public double tmY() { return tmY; }
}
