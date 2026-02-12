package com.smartstay.smartstay.dto.documents;

import java.util.List;

public record CustomerFiles(List<FileDetails> kycDoc,
                            List<FileDetails> checkinDoc,
                            List<FileDetails> otherDoc) {
}
