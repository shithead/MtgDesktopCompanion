if(dao.isSQL()) {
	printf("Executing db update on " + dao.getName());
	dao.executeQuery("ALTER TABLE transactions ADD sourceShopId VARCHAR(250)");
	dao.executeQuery("ALTER TABLE transactions ADD sourceShopName VARCHAR(250)");
	dao.executeQuery("ALTER TABLE transactions ADD typeTransaction VARCHAR(15)");
	dao.executeQuery("ALTER TABLE transactions ADD reduction DECIMAL(10,2)");
	
	
	dao.executeQuery("UPDATE announces set category='SET' where category='FULLSET'");
	dao.executeQuery("UPDATE orders set typeItem='SET' where typeItem='FULLSET'");
	
	printf("--done");
}
else
{
	printf("Your DAO is not SQL. Don't need to pass script");
}
