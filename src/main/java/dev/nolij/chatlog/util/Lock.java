package dev.nolij.chatlog.util;

public class Lock {
	
	public static class LeaseFailedException extends Exception {}
	public static class LeaseInvalidException extends Exception {}
	
	public static class Lease {
		
		private final Lock[] locks;
		
		private boolean valid = true;
		
		private Lease(Lock... locks) {
			this.locks = locks;
		}
		
		public boolean isValid() {
			return valid;
		}
		
		public synchronized void release() throws LeaseInvalidException {
			if (!valid)
				throw new LeaseInvalidException();
			
			valid = false;
			
			for (final Lock lock : locks) {
				lock.release();
			}
		}
		
	}
	
	public static class NullLease extends Lease {
		
		public NullLease() {}
		
		@Override
		public boolean isValid() {
			return false;
		}
		
		@Override
		public synchronized void release() throws LeaseInvalidException {
			throw new LeaseInvalidException();
		}
	}

	private boolean isLocked = false;

	public boolean isLocked() {
		return isLocked;
	}
	
	private synchronized void obtain() throws LeaseFailedException {
		if (isLocked)
			throw new LeaseFailedException();
		
		isLocked = true;
	}

	public synchronized Lease obtainLease() throws LeaseFailedException {
		obtain();
		return new Lease(this);
	}
	
	private synchronized void release() throws LeaseInvalidException {
		if (!isLocked)
			throw new LeaseInvalidException();
		
		isLocked = false;
	}
	
	public static Lease obtain(Lock... locks) throws LeaseFailedException {
		for (final Lock lock : locks) {
			try {
				lock.obtain();
			} catch (LeaseFailedException e) {
                for (final Lock leasedLock : locks) {
					if (leasedLock == lock)
						break;
                    try {
                        leasedLock.release();
                    } catch (LeaseInvalidException ignored) {}
                }
				throw e;
            }
        }
		
		return new Lease(locks);
	}

}