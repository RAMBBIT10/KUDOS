package co.edu.uco.kudos.auditoria;

import co.edu.uco.kudos.kudos.dominio.EstadoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria", indexes = @Index(name = "indice_auditoria_kudo", columnList = "kudo_id"))
public class AuditoriaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID kudoId;

    @Column(nullable = false)
    private String evento;

    private String canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAuditoria estado;

    @Column(nullable = false, length = 1000)
    private String detalle;

    @Column(nullable = false)
    private Instant creadoEn;

    public UUID getId() {
        return id;
    }

    public UUID getKudoId() {
        return kudoId;
    }

    public void setKudoId(UUID kudoId) {
        this.kudoId = kudoId;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public EstadoAuditoria getEstado() {
        return estado;
    }

    public void setEstado(EstadoAuditoria estado) {
        this.estado = estado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }
}
