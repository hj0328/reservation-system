package kr.or.connect.reservation.domain.category;

public class CategoryDaoSql {
	public static final String SELECT_CATEGORY = "SELECT count(*) AS count, c.category_id AS id, c.name "
											   + "FROM category c, product p "
											   + "WHERE c.category_id = p.category_id "
											   + "GROUP BY c.name, c.category_id ";
}
