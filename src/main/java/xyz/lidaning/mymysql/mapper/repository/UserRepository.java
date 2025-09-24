package xyz.lidaning.mymysql.mapper.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import xyz.lidaning.mymysql.domains.User;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {

  
}
