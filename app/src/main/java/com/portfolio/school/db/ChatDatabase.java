package com.portfolio.school.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.portfolio.school.model.DataResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Room 데이터베이스의 메인 클래스입니다.
 * 데이터베이스의 엔티티(테이블), 버전, DAO를 정의합니다.
 */
@Database(entities = {DataResponse.class}, version = 1, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {

    public abstract DataResponseDao chatDao();

    private static volatile ChatDatabase INSTANCE;

    /**
     * 데이터베이스 인스턴스를 가져오는 정적 메서드 (싱글턴 패턴).
     * 여러 스레드에서 동시에 접근해도 안전하게 하나의 인스턴스만 생성합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @return ChatDatabase의 싱글턴 인스턴스
     */
    public static ChatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    // 데이터베이스 인스턴스를 생성합니다.
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChatDatabase.class, "chat_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
