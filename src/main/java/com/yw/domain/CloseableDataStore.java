package com.yw.domain;

import com.yw.infrastructure.exception.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class CloseableDataStore implements Closeable {

    private final DataStore dataStore;

    public CloseableDataStore(IDataStoreTransferObject transfer) {
        try {
            if (Objects.isNull(transfer)) {
                throw new InternalServerException("데이터 스토어 속성 데이터 NULL");
            }

            this.dataStore = DataStoreFinder.getDataStore(transfer.transferData());

            if (Objects.isNull(this.dataStore)) {
                throw new InternalServerException("데이터 스토어 생성 실패");
            }

        } catch (IOException ioe) {
            throw new InternalServerException("데이터 스토어와의 연결 에러, ", ioe);
        }
    }

    public DataStore getCloseableDataStore() {
        return this.dataStore;
    }

    @Override
    public void close() throws IOException {
        this.dataStore.dispose();
    }
}