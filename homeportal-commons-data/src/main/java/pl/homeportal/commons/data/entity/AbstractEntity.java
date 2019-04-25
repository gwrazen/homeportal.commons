package pl.homeportal.commons.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Created by Grzegorz Wrażeń on 12.03.2017.
 */
@Setter
@Getter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class AbstractEntity<IDENTITY extends Number> implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    protected IDENTITY id;
}
