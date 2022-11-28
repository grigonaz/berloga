import { TestBed } from '@angular/core/testing';

import { ActiveAccountServiceService } from './active-account.service';

describe('ActiveAccountServiceService', () => {
  let service: ActiveAccountServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ActiveAccountServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
