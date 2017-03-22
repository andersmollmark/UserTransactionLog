import {TimeFilterService} from "../app/timefilter.service";
import {SelectedDate} from "../app/selectedDate";

describe('TimefilterService-tests', () => {

    let timefilterService;

    beforeEach(() => {
        timefilterService = new TimeFilterService();
    });

    describe('setLastSelectedTimefilterTo-test', () => {
        it('it shall return the date that was in setLast', () => {
            let testDate = new Date(2016, 10, 9);
            let expectedResult = SelectedDate.getFromDateParts(testDate);
            timefilterService.setLastSelectedTimefilterTo(testDate);
            let result = timefilterService.getLastSelectedTimefilterTo();
            expect(result).toEqual(expectedResult);
        });
    });

    describe('setLastSelectedTimefilterFrom-test', () => {
        it('it shall return the date that was in setLast', () => {
            let testDate = new Date(2016, 10, 9);
            let expectedResult = SelectedDate.getFromDateParts(testDate);
            timefilterService.setLastSelectedTimefilterFrom(testDate);
            let result = timefilterService.getLastSelectedTimefilterFrom();
            expect(result).toEqual(expectedResult);
        });
    });

    describe('resetTimefilter-test', () => {
        it('it shall reset all values', () => {
            spyOn(timefilterService, 'setSelectedTimefilterFrom');
            spyOn(timefilterService, 'setSelectedTimefilterTo');
            spyOn(timefilterService, 'setLastSelectedTimefilterFrom');
            spyOn(timefilterService, 'setLastSelectedTimefilterTo');

            let firstDateFromFile = new Date(2016, 10, 9);
            timefilterService.firstDateFromFile = firstDateFromFile;
            let lastDateFromFile = new Date(2017, 1, 9);
            timefilterService.lastDateFromFile = lastDateFromFile;
            timefilterService.filterQuery = "iosdjodji";
            timefilterService.resetTimefilter();
            expect(timefilterService.filterQuery).toBe("");
            expect(timefilterService.setSelectedTimefilterFrom).toHaveBeenCalledWith(firstDateFromFile);
            expect(timefilterService.setSelectedTimefilterTo).toHaveBeenCalledWith(lastDateFromFile);
            expect(timefilterService.setLastSelectedTimefilterFrom).toHaveBeenCalledWith(firstDateFromFile);
            expect(timefilterService.setLastSelectedTimefilterTo).toHaveBeenCalledWith(lastDateFromFile);

        });
    });

});

