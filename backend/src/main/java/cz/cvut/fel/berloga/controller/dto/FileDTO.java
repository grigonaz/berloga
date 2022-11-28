package cz.cvut.fel.berloga.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDTO  implements Serializable {

    private Long id;
    // example: hello.txt
    private String name;
    // example: 6d54f65s4d5c4s54e4r56df6
    private String pseudoName;
    // in bytes
    private Long size;
    private Long ownerID;

}
