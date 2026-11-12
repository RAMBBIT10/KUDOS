package co.edu.uco.kudos.kudos.infraestructura.persistencia;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "kudo", indexes = @Index(name = "indice_kudo_destinatario_fecha", columnList = "destinatario_id, creado_en"))
public class KudoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID autorId;

    @Column(nullable = false)
    private UUID destinatarioId;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "kudo", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<AdjuntoEntidad> adjuntos = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public UUID getAutorId() {
        return autorId;
    }

    public void setAutorId(UUID autorId) {
        this.autorId = autorId;
    }

    public UUID getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(UUID destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public List<AdjuntoEntidad> getAdjuntos() {
        return adjuntos;
    }
}
