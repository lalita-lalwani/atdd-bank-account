package org.xpdojo.bank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xpdojo.bank.Money.ZERO;
import static org.xpdojo.bank.Money.amountOf;
import static org.xpdojo.bank.Transaction.Deposit;
import static org.xpdojo.bank.Transaction.Deposit.deposit;
import static org.xpdojo.bank.Transaction.Deposit.depositOf;
import static org.xpdojo.bank.Transaction.Tally.tally;
import static org.xpdojo.bank.Transaction.Withdraw.withdraw;

class TransactionTest {

	@Test
	void depositShouldAddDepositAmount() {
		Deposit initialDeposit = depositOf(ZERO);
		Transaction result = deposit(amountOf(10)).against(initialDeposit);
		assertThat(result.amount(), is(amountOf(10)));
	}

	@Test
	void withdrawalShouldSubtractWithdrawalAmount() {
		Deposit initialDeposit = deposit(amountOf(10));
		Transaction result = withdraw(amountOf(10)).against(initialDeposit);
		assertThat(result.amount(), is(ZERO));
	}
	
	@ParameterizedTest(name = "deposit of {0} and {1} should sum to {2}")
	@CsvSource({
			"10, 20, 30",
			"20, 10, 30"
	})
	void twoDepositsSumToTotal(long amount1, long amount2, long expectedSum) {
		assertThat(deposit(amountOf(amount1)).against(deposit(amountOf(amount2))).amount(), is(amountOf(expectedSum)));
	}

	@Test
	void withdrawalOfMoreThanInitialDepositShouldGiveNegativeTotal() {
		assertThat(withdraw(amountOf(5)).against(deposit(amountOf(2))).amount(), is(amountOf(-3)));
	}

	@Test
	void mixOfDepositsAndWithdrawalsShouldSumToTotalOfAllTransactions() {
		Deposit initialDeposit = deposit(amountOf(10));
		Transaction result =
				deposit(amountOf(5)).against(
					withdraw(amountOf(10)).against(
						initialDeposit));
		assertThat(result.amount(), is(amountOf(5)));
	}

	@Test
	void moreComplexMixOfDepositsAndWithdrawalsShouldSumToTotalOfAllTransactions() {
		Deposit initialDeposit = deposit(amountOf(2));
		Transaction result =
			withdraw(amountOf(5)).against(
				deposit(amountOf(20)).against(
					deposit(amountOf(10)).against(
							initialDeposit)));
		assertThat(result.amount(), is(amountOf(27)));
	}

	@Test
	void reducingAStreamOfTransactionsShouldGiveCurrentBalance() {
		List<Transaction> transactions = asList(
			deposit(amountOf(10)),
			deposit(amountOf(20)),
			withdraw(amountOf(5)),
			withdraw(amountOf(5)),
			deposit(amountOf(3))
		);
		Optional<Transaction> reduce = transactions.stream().reduce((a, b) -> b.against(a));
		assertThat(reduce.get().amount(), is(amountOf(23)));
	}
	
	@Test
	void tallyingTransactionCanNotBeMadeAgainstOthers() {
		assertThat(tally(amountOf(100)).against(withdraw(amountOf(50))).amount(), is(amountOf(100)));
		assertThat(tally(amountOf(100)).against(deposit(amountOf(50))).amount(), is(amountOf(100)));
	}
	
	@Test
	void tallyingTransactionCanStillBeAppliedToAnother() {
		assertThat(withdraw(amountOf(50)).against(tally(amountOf(100))).amount(), is(amountOf(50)));
		assertThat(deposit(amountOf(50)).against(tally(amountOf(100))).amount(), is(amountOf(150)));
	}
}