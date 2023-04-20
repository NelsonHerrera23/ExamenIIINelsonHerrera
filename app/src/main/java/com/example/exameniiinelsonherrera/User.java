package com.example.exameniiinelsonherrera;


public class User {
    private String IdSitio;
    private String Descripcion;
    private String Fecha;
    private String Imagen;
    private String Video;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String idSitio, String descripcion, String fecha, String imagen, String video) {
        IdSitio = idSitio;
        Descripcion = descripcion;
        Fecha = fecha;
        Imagen = imagen;
        Video = video;
    }

    public String getIdSitio() {
        return IdSitio;
    }

    public void setIdSitio(String idSitio) {
        IdSitio = idSitio;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }

    public String getVideo() {
        return Video;
    }

    public void setVideo(String video) {
        Video = video;
    }
}
