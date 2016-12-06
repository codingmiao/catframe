package org.wowtools.common.pcm;

import java.util.concurrent.atomic.AtomicInteger;

import org.wowtools.common.pcm.ltp.PcmTaskLtp;
import org.wowtools.common.pcm.ltp.ProducerLtp;

public class PcmTest {

	public static void main(String[] args) {
		ProducerLtp<Integer> producer = new ProducerLtp<Integer>() {
			AtomicInteger i = new AtomicInteger(0);

			@Override
			public Integer produce() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				int obj = i.addAndGet(1);
				System.out.println("生产"+obj);
				return obj;
			}

			@Override
			public boolean isFinish() {
				return i.get() == 10;
			}
		};
		Customer<Integer> customer = new Customer<Integer>() {

			@Override
			public void consume(Integer obj) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				System.out.println("消费"+obj);
			}
		};
		PcmTaskLtp<Integer> task = new PcmTaskLtp<>(producer, customer, 5, 1000);
		task.startTask(true);
		System.out.println("end");
	}

}
