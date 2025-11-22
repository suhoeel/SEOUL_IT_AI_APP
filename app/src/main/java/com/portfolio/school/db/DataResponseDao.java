package com.portfolio.school.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.portfolio.school.model.DataResponse;

import java.util.List;

/**
 * 데이터베이스에 접근하여 채팅 데이터를 관리하는 인터페이스입니다.
 * Room 라이브러리가 이 인터페이스의 구현체를 자동으로 생성합니다.
 */
@Dao
public interface DataResponseDao {

    /**
     * 새로운 채팅 메시지를 데이터베이스에 삽입합니다.
     *
     * @param chat 저장할 Chat 객체
     */
    @Insert
    void insert(DataResponse chat);

    /**
     * 데이터베이스에 저장된 모든 채팅 기록을 시간순(오름차순)으로 가져옵니다.
     *
     * @return 모든 Chat 객체의 리스트
     */
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    List<DataResponse> getAll();

    /**
     * (선택 사항) 데이터베이스의 모든 채팅 기록을 삭제합니다.
     */
    @Query("DELETE FROM chat_history")
    void deleteAll();
}
