package com.hassanassowe.raspberrycam.Classes;

public class CameraListEntity {
    public int mimagelayanan;
    private String mTint;
    private String mtextlayanan;
    private String mtextdokter;

    public CameraListEntity(int imagelayanan, String textlayanan, String textdokter, String tint) {
        mimagelayanan = imagelayanan;
        mtextlayanan = textlayanan;
        mtextdokter = textdokter;
        mTint = tint;
    }


    public int getMimagelayanan() {
        return mimagelayanan;
    }

    public String getMtextlayanan() {
        return mtextlayanan;
    }

    public String getMtextdokter() {
        return mtextdokter;
    }

    public String getTint() {
        return mTint;
    }

    public void setMimagelayanan(int image) {
        mimagelayanan = image;
    }

    public void setTint(String tint) {
        mTint = tint;
    }

    public void setMtextlayanan(String text) {
        mtextlayanan = text;
    }

    public void setMtextdokter(String text) {
        mtextdokter = text;
    }


}
