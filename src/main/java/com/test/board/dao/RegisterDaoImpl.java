package com.test.board.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.test.board.domain.MemberVO;

public class RegisterDaoImpl implements RegisterDao {
	private SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	public RegisterDaoImpl(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}
	@Override
	public void register(MemberVO memberVO) {
		
		sqlSessionTemplate.insert("insertMember", memberVO);
		
		
	}
}
