package kr.hhplus.be.server.interfaces.api.bestseller.constants;

public enum BestSellerPeriod {
	DAILY(1),
	WEEKLY(7),
	MONTHLY(30);

	private final int days;

	BestSellerPeriod(int days) {
		this.days = days;
	}

	public int days() {
		return days;
	}
}
